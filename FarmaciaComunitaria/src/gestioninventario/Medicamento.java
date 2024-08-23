/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestioninventario;


/**
 *
 * @author user1
 */
public class Medicamento {
    
    private static int codigoBase = 0;
    private int codigo;
    private String nombre;
    private String tipo;
    private int stock;
    private int cantidad;
    private int precio;

    //Constructor Base
    public Medicamento(String nombre, String tipo, int stock, int precio) {
        Medicamento.codigoBase += 1;
        this.codigo = Medicamento.codigoBase;
        this.nombre = nombre;
        this.tipo = tipo;
        this.stock = stock;
        this.precio = precio;
        this.cantidad = 0;
    }
    //Constructor Vacío
    public Medicamento() {
        Medicamento.codigoBase += 1;
        this.codigo = Medicamento.codigoBase;
        this.nombre = "";
        this.tipo = "";
        this.stock = 0;
        this.precio = 0;
        this.cantidad = 0;
    }
    
    //Constructor Medicamento singular
    public Medicamento(Medicamento original, int cantidad) {
        this.codigo = original.getCodigo();
        this.nombre = original.getNombre();
        this.tipo = original.getTipo();
        this.precio = original.getPrecio();
        this.stock = 0;
        this.cantidad = cantidad;
    }
    
    //Getters
    public static int getCodigoBase() {
        return codigoBase;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public int getStock() {
        return stock;
    }

    public int getPrecio() {
        return precio;
    }
    
    //Setters
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }
    
    
    private boolean validarSaldoCliente(Cliente cliente){
        return cliente.getCartera() > 0;
    }
    
    public void vender(Cliente cliente){
        if (this.stock > 0 && validarSaldoCliente(cliente)) { 
            int saldoCliente = cliente.getCartera();
            if (saldoCliente >= this.precio){
                int nuevoSaldoCliente = saldoCliente - this.precio;
                cliente.setCartera(nuevoSaldoCliente);
                this.stock -= 1;
                cliente.setMedicamentoAdquirido(new Medicamento(this, 1));
                
                System.out.println("Has comprado (1) " + this.nombre + " por el valor de: $" + this.precio + "\n" 
                   + "Tu saldo ahora es de: $" + cliente.getCartera());

            }
            else{
                System.out.println("Lo siento, no tienes saldo suficiente. :/");
            }
        }
    }

    
}


