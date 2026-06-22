package com.lms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "instructors")
@Getter
@Setter
@SQLDelete(sql = "UPDATE instructors SET is_deleted = true WHERE id=?")
@Where(clause = "is_deleted=false")
public class Instructor extends User {

    @Column(nullable = false)
    private String expertise;

    @Column(length = 1000)
    private String bio;
}
