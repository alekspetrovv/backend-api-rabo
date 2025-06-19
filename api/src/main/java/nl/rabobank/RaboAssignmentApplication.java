package nl.rabobank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "nl.rabobank.mongo.repository")
public class RaboAssignmentApplication
{
    public static void main(final String[] args)
    {
        SpringApplication.run(RaboAssignmentApplication.class, args);
    }
}
