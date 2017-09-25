package de.andre.data.db;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity(name = "DBExperiment")
@Table(name = "EXPERIMENT")
@Data
public class DBExperiment {
    @Id
    @Column(name = "ID_EXPERIMENT")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "DRIVER", length = 50, nullable = false)
    private String driver;
}
