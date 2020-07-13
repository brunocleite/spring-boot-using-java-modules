package com.okta.developer.animals.bird;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class BirdPersistence {

    private BirdRepository birdRepository;

    @Autowired
    public BirdPersistence(BirdRepository birdRepository) {
        this.birdRepository = birdRepository;
    }

    @PostConstruct
    void postConstruct(){
        birdRepository.deleteAll(); //Clean up DB so there is no leftover data between runs
        Bird sampleBird = new Bird();
        sampleBird.setSpecie("Hummingbird");
        sampleBird.setSize("small");
        save(sampleBird);
    }

    public void save(Bird bird) {
        birdRepository.save(bird);
    }

    public List<Bird> get() {
        return birdRepository.findAll();
    }

}
