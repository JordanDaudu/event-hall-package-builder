package com.eventhall.entity;

import jakarta.persistence.*;

/*
 * Entity = a Java class mapped to a database table.
 *
 * This Customer class represents the customer table in the database.
 * Each object of this class represents one row in that table.
 */
@Entity
public class Customer {

    /*
     * @Id marks this field as the primary key of the table.
     * A primary key uniquely identifies each row.
     */
    @Id

    /*
     * @GeneratedValue tells the database to generate the id automatically.
     *
     * GenerationType.IDENTITY usually means PostgreSQL will use an auto-incrementing
     * identity column for this value.
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * These fields become columns in the customer table.
     * Since there is no @Column customization here, JPA uses default column names:
     * name and email.
     */
    private String name;
    private String email;

    /*
     * Required by JPA.
     *
     * When JPA reads rows from the database, it needs a no-argument constructor
     * so it can create the object first and then fill in the fields.
     *
     * protected means normal application code should use the public constructor,
     * but JPA is still allowed to use this one.
     */
    protected Customer() {
    }

    /*
     * Constructor used by our application when creating a new customer
     * from a quote request.
     *
     * We do not set id manually because the database generates it.
     */
    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}