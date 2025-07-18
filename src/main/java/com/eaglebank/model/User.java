package com.eaglebank.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(generator = "user-id-generator")
    @GenericGenerator(name = "user-id-generator", strategy = "com.eaglebank.model.UserIdGenerator")
    private String id;

    private String name;

    private String email;

    private String password;

    private OffsetDateTime createdTimestamp;

    private OffsetDateTime updatedTimestamp;

    private String phoneNumber;

    @Embedded
    private Address address;

    public User(String name, String email, String password, String phoneNumber, Address address) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}
