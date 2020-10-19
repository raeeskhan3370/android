package com.clubecerto.apps.app.classes;

public class Estable {


     String Codigo ,
              Nome ,
              Destaque ,
              Palavras_chave ,
             Marca ,
              Capa ,
             CategoriaCodigo ,
             CategoriaNome ,
             Beneficio ,
             Ativo ;

    public Estable(String codigo, String nome, String destaque, String palavras_chave, String marca, String capa, String categoriaCodigo, String categoriaNome, String beneficio, String ativo) {
        Codigo = codigo;
        Nome = nome;
        Destaque = destaque;
        Palavras_chave = palavras_chave;
        Marca = marca;
        Capa = capa;
        CategoriaCodigo = categoriaCodigo;
        CategoriaNome = categoriaNome;
        Beneficio = beneficio;
        Ativo = ativo;
    }

    public String getCodigo() {
        return Codigo;
    }

    public void setCodigo(String codigo) {
        Codigo = codigo;
    }

    public String getNome() {
        return Nome;
    }

    public void setNome(String nome) {
        Nome = nome;
    }

    public String getDestaque() {
        return Destaque;
    }

    public void setDestaque(String destaque) {
        Destaque = destaque;
    }

    public String getPalavras_chave() {
        return Palavras_chave;
    }

    public void setPalavras_chave(String palavras_chave) {
        Palavras_chave = palavras_chave;
    }

    public String getMarca() {
        return Marca;
    }

    public void setMarca(String marca) {
        Marca = marca;
    }

    public String getCapa() {
        return Capa;
    }

    public void setCapa(String capa) {
        Capa = capa;
    }

    public String getCategoriaCodigo() {
        return CategoriaCodigo;
    }

    public void setCategoriaCodigo(String categoriaCodigo) {
        CategoriaCodigo = categoriaCodigo;
    }

    public String getCategoriaNome() {
        return CategoriaNome;
    }

    public void setCategoriaNome(String categoriaNome) {
        CategoriaNome = categoriaNome;
    }

    public String getBeneficio() {
        return Beneficio;
    }

    public void setBeneficio(String beneficio) {
        Beneficio = beneficio;
    }

    public String getAtivo() {
        return Ativo;
    }

    public void setAtivo(String ativo) {
        Ativo = ativo;
    }
}
