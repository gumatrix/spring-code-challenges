package com.cecilireid.springchallenges;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.isEmpty;


@RestController
@RequestMapping("cateringJobs")
public class CateringJobController {

    private static final String IMAGE_API = "https://foodish-api.herokuapp.com";
    private final CateringJobRepository cateringJobRepository;

    public CateringJobController(CateringJobRepository cateringJobRepository, WebClient.Builder webClientBuilder) {
        this.cateringJobRepository = cateringJobRepository;
    }

    @GetMapping
    @ResponseBody
    public List<CateringJob> getCateringJobs() {
        return cateringJobRepository.findAll();
    }

    @GetMapping("/{id}")
    @ResponseBody
    public CateringJob getCateringJobById(@PathVariable Long id) {
        if (cateringJobRepository.existsById(id)) {
            return cateringJobRepository.findById(id).get();
        } else {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/findByStatus")
    public List<CateringJob> getCateringJobsByStatus(@RequestParam Status status) {
        return cateringJobRepository.findByStatus(status);
    }

    @PostMapping
    @ResponseBody
    public CateringJob createCateringJob(@RequestBody CateringJob job) {
        return cateringJobRepository.save(job);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public CateringJob updateCateringJob(@RequestBody CateringJob cateringJob, @PathVariable Long id) {
        if (cateringJobRepository.existsById(id)) {
            cateringJob.setId(id);
            return cateringJobRepository.save(cateringJob);
        } else {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{id}")
    @ResponseBody
    public CateringJob patchCateringJob(@PathVariable Long id, @RequestBody JsonNode json) {
        final Optional<CateringJob> result = cateringJobRepository.findById(id);

        if (result.isPresent()) {
            final CateringJob cateringJob = result.get();
            final JsonNode menu = json.get("menu");

            if (menu != null) {
                cateringJob.setMenu(menu.asText());
                return cateringJobRepository.save(cateringJob);
            } else {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Menu is required");
            }
        } else {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
    }

    public Mono<String> getSurpriseImage() {
        return null;
    }

    @ExceptionHandler(HttpClientErrorException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String handleClientException() {
        return "Not found: Please try again";
    }
}
