# API Music Recommender Report

## Challenges Faced
**Challenge 1: Save/Load**  
- Problem: Needed to figure out Save/Loading functionality  
- Solution: Created a log file that can be loaded to the GUI upon pressing the history button. The log file gets updated at the same time the request gets pushed to the GUI. This way, the log.txt and log gui can be viewed separately. Log.txt could also be used for terminal jUnit testing in the future.  

**Challenge 2: Restructuring to fit MVC format**  
- Problem: Needed to figure out which classes should go where to create a cleaner, more readable structure.  
- Solution: Followed along with lab and assignment guidelines to design a sensible layout.   

**Challenge 3: Generate a library of songs for the random recommendations to come from**  
- Problem: There was no method we could find to randomly shuffle all possible songs that exist and pull recommendations from there for our “Random Recommendation” strategy.   
- Solution: Cumulated our own library of songs built from the overall collection of generated recommendations in the current session.   

## Design Pattern Justifications
**Strategy Pattern:** Our program has the ability to switch between two different strategies: 
- The first strategy utilized is the similarity strategy, where recommendations are made based on how similar the song is to others. This strategy is considered formal, as it uses the same formula to determine song similarity based on each song’s data.
- The next strategy is the Random Strategy, and it provides random recommendations from the cache of accumulated song recommendations generated so far. This allows it to account for the tastes of the user while still promoting exploration of music that the user may not have yet explicitly known of. The random strategy could be paired with other strategies in the future to provide more tailored recommendations to the user. This strategy may be considered as creative due to the fact that it won’t always return consistent results. This is mainly because it relies on the user only entering songs/artists that they like. On top of that, repetitive searches for the same songs could lead to skewed recommendation data. However, some could argue that this feature ensures that songs put on repeat are prioritized, which could make more of a case for it being a creative strategy rather than a formal one due to its ability to solve a problem in an unconventional way.
  
**Factory Pattern:** There is only one pattern here used for the API calls. This is because due to the nature of the music recommendation assignment, most API calls here end up taking a very similar form.
  
**Observer Pattern:** The first UI update can happen after the user presses the enter key or presses search recommendations upon entering artist data. The second way the UI can update is the song log GUI, which updates as soon as the user presses the search button. This popup makes its appearance through the selection of the history button in the lower right corner of the main GUI window.

## AI Usage 
Music recommender lab was written and modified by claude to use the Last fm API. Api project code is built off of that foundation. Some of the tests were also built with some AI guidance. Guidance for RandomStrategy logic and implementation utilized AI tools as well. 

## Time Spent: ~25 hours
