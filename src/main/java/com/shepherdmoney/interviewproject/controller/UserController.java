package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    // wire in the user repository (~ 1 line)
    @Autowired
    private UserRepository userRepository;

    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        try {
            // Create an user entity with information given in the payload, store it in the
            // database
            // and return the id of the user in 200 OK response
            User user = new User();

            // Set fields from payload to user
            user.setName(payload.getName());
            user.setEmail(payload.getEmail());

            userRepository.save(user);

            // Return user's id
            return new ResponseEntity<>(user.getId(), HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        try {
            // Return 200 OK if a user with the given ID exists, and the deletion is
            // successful
            // Return 400 Bad Request if a user with the ID does not exist
            // The response body could be anything you consider appropriate
            Optional<User> user = userRepository.findById(userId);

            if (!user.isPresent()) {
                return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
            }

            userRepository.deleteById(userId);
            return new ResponseEntity<>("Deletion succeeded", HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
