package nl.rabobank.mongo.repository;

import lombok.RequiredArgsConstructor;
import nl.rabobank.mongo.document.user.UserDocument;
import nl.rabobank.user.IUserRepository;
import nl.rabobank.user.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MongoUserRepositoryAdapter implements IUserRepository {

    private final UserRepository mongoDbUserRepository;
    private final ModelMapper modelMapper;

    @Override
    public User save(User user) {
        UserDocument userDocument = modelMapper.map(user, UserDocument.class);
        UserDocument savedDocument = mongoDbUserRepository.save(userDocument);
        return modelMapper.map(savedDocument, User.class);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return mongoDbUserRepository.findByEmail(email)
                .map(userDocument -> modelMapper.map(userDocument, User.class));
    }

    @Override
    public Optional<User> findById(String id) {
        return mongoDbUserRepository.findById(id)
                .map(userDocument -> modelMapper.map(userDocument, User.class));
    }
}