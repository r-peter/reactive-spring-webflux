package com.reactivespring.service;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MovieInfoService {

    private MovieInfoRepository movieInfoRepository;

    public MovieInfoService(MovieInfoRepository movieInfoRepository) {
        this.movieInfoRepository = movieInfoRepository;
    }

    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
        return movieInfoRepository.save(movieInfo);
    }

    public Flux<MovieInfo> getAllMovieInfo() {
        return movieInfoRepository.findAll();
    }

    public Mono<MovieInfo> getMovieInfoById(String id) {
        return movieInfoRepository.findById(id);
    }

    public Flux<MovieInfo> getMovieInfoByYear(Integer year) {
        return movieInfoRepository.findByYear(year);
    }

    public Mono<MovieInfo> updateMovieInfo(String id, MovieInfo updatedMovieInfo) {
        return movieInfoRepository.findById(id)
                .flatMap(existingMovieInfo -> {
                    existingMovieInfo.setCast(updatedMovieInfo.getCast());
                    existingMovieInfo.setName(updatedMovieInfo.getName());
                    existingMovieInfo.setYear(updatedMovieInfo.getYear());
                    existingMovieInfo.setReleaseDate(updatedMovieInfo.getReleaseDate());
                    return movieInfoRepository.save(existingMovieInfo);
                });
    }

    public Mono<Void> deleteMovieInfoById(String id) {
       /* return movieInfoRepository.findById(id)
                .flatMap(existingMovieInfo -> {
                    movieInfoRepository.delete(existingMovieInfo);
                });*/
        return movieInfoRepository.deleteById(id);
    }


}
