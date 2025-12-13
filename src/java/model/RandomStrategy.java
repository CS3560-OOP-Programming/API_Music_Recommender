package model;

import java.util.*;

public class RandomStrategy implements RecommendationStrategy{
    private final List<Track> allTracks;
    private final Random rndm = new Random();

    public RandomStrategy(List<Track> allTracks) {
        this.allTracks = allTracks;
    }

    @Override
    public List<Track> recommend(List<Track> userTracks, int count) {
        if (allTracks == null || allTracks.isEmpty()) {
            return Collections.emptyList();
        }

        List<Track> copy = new ArrayList<>(allTracks);
        Collections.shuffle(copy);

        List<Track> result = copy.subList(0,Math.min(count, copy.size()));

        //Make sure the match score is only displayed in the case that
        // we are using the similarity based recommendation mode
        for(Track t : result){
            t.setMatchScore(0.0);
        }

        return result;
    }

    @Override
    public String getStrategyName() {
        return "Random Recommendation";
    }
}
