package com.clubecerto.apps.app.classes;

import java.io.Serializable;

public class Companies implements Serializable {


  String  Codigo , Nome  ,   Imagem, Tipo,CorTextoCartaoVirtual,CorBackgroundCabecalho,CorItensCabecalho,Frente,Verso,Ativo;

    public Companies(String codigo, String nome, String imagem, String tipo, String corTextoCartaoVirtual, String corBackgroundCabecalho, String corItensCabecalho, String frente, String verso, String ativo) {
        Codigo = codigo;
        Nome = nome;
        Imagem = imagem;
        Tipo = tipo;
        CorTextoCartaoVirtual = corTextoCartaoVirtual;
        CorBackgroundCabecalho = corBackgroundCabecalho;
        CorItensCabecalho = corItensCabecalho;
        Frente = frente;
        Verso = verso;
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

    public String getImagem() {
        return Imagem;
    }

    public void setImagem(String imagem) {
        Imagem = imagem;
    }

    public String getTipo() {
        return Tipo;
    }

    public void setTipo(String tipo) {
        Tipo = tipo;
    }

    public String getCorTextoCartaoVirtual() {
        return CorTextoCartaoVirtual;
    }

    public void setCorTextoCartaoVirtual(String corTextoCartaoVirtual) {
        CorTextoCartaoVirtual = corTextoCartaoVirtual;
    }

    public String getCorBackgroundCabecalho() {
        return CorBackgroundCabecalho;
    }

    public void setCorBackgroundCabecalho(String corBackgroundCabecalho) {
        CorBackgroundCabecalho = corBackgroundCabecalho;
    }

    public String getCorItensCabecalho() {
        return CorItensCabecalho;
    }

    public void setCorItensCabecalho(String corItensCabecalho) {
        CorItensCabecalho = corItensCabecalho;
    }

    public String getFrente() {
        return Frente;
    }

    public void setFrente(String frente) {
        Frente = frente;
    }

    public String getVerso() {
        return Verso;
    }

    public void setVerso(String verso) {
        Verso = verso;
    }

    public String getAtivo() {
        return Ativo;
    }

    public void setAtivo(String ativo) {
        Ativo = ativo;
    }
}
