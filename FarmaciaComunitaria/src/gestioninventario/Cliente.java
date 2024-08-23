/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestioninventario;

/**
 *
 * @author user1
 */
public class Cliente {
    private static int numeroClienteBase = 0;
    private int numeroCliente;
    private String nombre;
    private Medicamento medicamentoAdquirido;
    private int Cartera; //keeps track of money

    //Constructor
    public Cliente(String nombre, int Cartera) {
        Cliente.numeroClienteBase += 1;
        this.numeroCliente = Cliente.numeroClienteBase;
        this.nombre = nombre;
        this.Cartera = Cartera;
    }
    
    //Getters

    public String getNombre() {
        return nombre;
    }

    public Medicamento getMedicamentoAdquirido() {
        return medicamentoAdquirido;
    }

    public int getCartera() {
        return Cartera;
    }
    
    //Setters

    public void setMedicamentoAdquirido(Medicamento medicamentoAdquirido) {
        this.medicamentoAdquirido = medicamentoAdquirido;
    }

    public void setCartera(int Cartera) {
        this.Cartera = Cartera;
    }
    
    
    
    
    public void realizarCompra(Medicamento medicamento){
        medicamento.vender(this);
    }
    
    
}
