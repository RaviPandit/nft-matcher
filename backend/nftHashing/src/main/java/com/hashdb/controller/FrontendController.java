package com.hashdb.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.batik.transcoder.TranscoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hashdb.entities.NFT;
import com.hashdb.entities.NftHash;
import com.hashdb.repositories.NftHashRepository;
import com.hashdb.repositories.NftRepository;
import com.hashdb.util.HashingUtility;
import com.hashdb.util.convertToPng;

import dev.brachtendorf.jimagehash.hash.Hash;

@RestController
@CrossOrigin() 
public class FrontendController {

@Autowired
NftHashRepository nftHashRepository;

@Autowired
NftRepository nftRepository;

	
@PostMapping("/find/any/similar/image")
	public ResponseEntity<List<SimilarNft>> similarImage(@RequestParam("image")  MultipartFile file)
			throws IOException, TranscoderException {		

			 BufferedImage hashImage;
		        if (file.getOriginalFilename().endsWith(".svg")) {
		            hashImage = convertToPng.convertSvgToPng(file.getInputStream());
		        } else {
		            hashImage = ImageIO.read(file.getInputStream());
		        }		
		        
		     Hash hash = HashingUtility.hashImage(hashImage);		
				
		        List<Integer> matchedNFTs = nftHashRepository.findAll()
		                .stream()
		                .filter(nftHash -> {
		                    double similarityScore = (hash.getHashValue().xor(nftHash.getHashValue()).bitCount()) / (double) nftHash.getLen();
		                    return similarityScore < 0.190;
		                })
		                .map(NftHash::getNftId)
		                .filter(Objects::nonNull)
		                .collect(Collectors.toList());
		        List<SimilarNft> similarNFTs = matchedNFTs.stream()
		        	    .map((Integer nftId) -> {
		        	        Optional<NFT> optionalNft = nftRepository.findByNftId(nftId);
		        	        if (optionalNft.isPresent()) {
		        	            NFT nft = optionalNft.get();
//		        	            similarNft.Set
		        	            return new SimilarNft(nft.getId(), nft.getNftId(),nft.getImageOriginalUrl(), nft.getTokenId(), nft.getAddress(), nft.getTokenMetadata());
		        	     	   }
							return null;
		        	    })
		        	    .filter(Objects::nonNull)
		        	    .collect(Collectors.toList());

		      	
//        System.out.println("Total Match Found: " + similarNFTs.size());
    
	    return ResponseEntity.ok().body(similarNFTs);
		
	}
}

