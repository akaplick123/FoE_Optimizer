package de.andre.data.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "DBExperimentParameter")
@Table(name = "EXP_PARAM")
public class DBExperimentParameter {
    @Id
    @Column(name = "ID_EXP_PARAM")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @JoinColumn(name = "ID_EXPERIMENT")
    @ManyToOne(optional = false)
    private DBExperiment experiment;

    @Column(name = "PARAM_KEY", length = 30, nullable = false)
    private String key;

    @Column(name = "PARAM_VALUE", length = 30, nullable = false)
    private String value;
}
