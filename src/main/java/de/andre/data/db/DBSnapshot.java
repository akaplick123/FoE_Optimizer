package de.andre.data.db;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Entity(name = "DBSnapshot")
@Table(name = "EXP_SNAPSHOT")
@Data
public class DBSnapshot {

    @Id
    @Column(name = "ID_EXP_SNAPSHOT")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @JoinColumn(name = "ID_EXPERIMENT")
    @ManyToOne(optional = false)
    private DBExperiment experiment;

    @Column(name = "FIELD", length = 100, nullable = false)
    private String encodedField;

    @Column(name = "HOUSES", nullable = false)
    private int numberOfHouses;

    @Column(name = "TILES_USED", nullable = false)
    private int tilesOccupied;

    @Column(name = "SNAPSHOT_TIME", nullable = false)
    private LocalDateTime timestamp;
}
