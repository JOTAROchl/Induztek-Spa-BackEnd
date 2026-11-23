package cl.duocuc.induztek.protocols.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "protocolos")
public class Protocolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String equipo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 40)
    private String estado;

    @Column(length = 5000)
    private String observaciones;

    @Column(name = "correo_usuario", nullable = false, length = 254)
    private String correoUsuario;

    @Column(length = 20)
    private String temperatura;

    @Column(length = 20)
    private String humedad;

    @Column(nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(nullable = false)
    private Instant actualizadoEn;

    protected Protocolo() {
    }

    public Protocolo(String equipo, LocalDate fecha, String estado, String observaciones,
                     String correoUsuario, String temperatura, String humedad) {
        this.equipo = equipo;
        this.fecha = fecha;
        this.estado = estado;
        this.observaciones = observaciones;
        this.correoUsuario = correoUsuario;
        this.temperatura = temperatura;
        this.humedad = humedad;
    }

    @PrePersist
    void alCrear() {
        creadoEn = Instant.now();
        actualizadoEn = creadoEn;
    }

    @PreUpdate
    void alActualizar() {
        actualizadoEn = Instant.now();
    }

    public void actualizar(String equipo, LocalDate fecha, String estado, String observaciones,
                           String temperatura, String humedad) {
        this.equipo = equipo;
        this.fecha = fecha;
        this.estado = estado;
        this.observaciones = observaciones;
        this.temperatura = temperatura;
        this.humedad = humedad;
    }

    public Long getId() {
        return id;
    }

    public String getEquipo() {
        return equipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public String getCorreoUsuario() {
        return correoUsuario;
    }

    public String getTemperatura() {
        return temperatura;
    }

    public String getHumedad() {
        return humedad;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }
}
