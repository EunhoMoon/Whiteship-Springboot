package whiteship;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class DemoRestclientApplication {

	@Autowired
	WebClient.Builder webClientBuild;
	
	@Autowired
	RestTemplateBuilder restTemplateBuilder;
	
	public static void main(String[] args) {
		SpringApplication.run(DemoRestclientApplication.class, args);
	}
	
	@Bean
	public ApplicationRunner applicationRunner() {
		return args -> {
			
			WebClient webClient = webClientBuild.baseUrl("https://api.github.com").build();
			
			
			// NonBlocking 코드 : 작성한 순서에 상관 없이 동시에 실행
			Flux<GithubRepository> repos = webClient.get().uri("/users/EunhoMoon/repos")
					.retrieve()
					.bodyToFlux(GithubRepository.class);
			
			Mono<GithubCommit[]> commitsMono = webClient.get().uri("/repos/EunhoMoon/Springboot-JPA-Blog/commits")
					.retrieve()
					.bodyToMono(GithubCommit[].class);
			
			repos.subscribe(r -> System.out.println("repo : " + r.getUrl()));
			
			commitsMono.doOnSuccess(ca -> {
				Arrays.stream(ca).forEach(c -> {
					System.out.println("commit : " + c.getSha());
				});
			}).subscribe();
			
			// Blocking 코드 : 작성한 순서에 따라 실행
			RestTemplate restTemplate = restTemplateBuilder.build();
			
			GithubRepository[] result = restTemplate.getForObject("https://api.github.com/users/EunhoMoon/repos", GithubRepository[].class);
			Arrays.stream(result).forEach(r -> {
				System.out.println("repo : " + r.getUrl());
			});
			
			GithubCommit[] commits = restTemplate.getForObject("https://api.github.com/repos/EunhoMoon/Springboot-JPA-Blog/commits", GithubCommit[].class);
			Arrays.stream(commits).forEach(c -> {
				System.out.println("commit : " + c.getSha());
			});
		};
	}
}
