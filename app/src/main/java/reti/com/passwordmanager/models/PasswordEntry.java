package reti.com.passwordmanager.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.Serializable;

@SuppressWarnings("serial")
@Entity @Keep
public class PasswordEntry implements Serializable{

    @NotNull
    public String dominio;
    @NotNull
    public String username;
    @NotNull
    public String password;


    public PasswordEntry(String dominio,String username,String password){
        this.dominio = dominio;
        this.username=username;
        this.password=password;
    }

    public String getDominio(){
        return this.dominio;
    }
    public String getUsername(){
        return this.username;
    }
    public String getPassword(){
        return this.password;
    }
    public void setDominio(String dominio){
        this.dominio = dominio;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public void setPassword(String password){ this.password = password; }
}
