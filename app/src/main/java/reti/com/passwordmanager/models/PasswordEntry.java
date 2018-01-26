package reti.com.passwordmanager.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.Serializable;

@SuppressWarnings("serial")
@Entity
public class PasswordEntry implements Serializable{

    @Id(autoincrement = true)
    private long id;

    @Index
    private String dominio, username,password,category;

/*    @NotNull
    public String dominio;
    @NotNull
    public String username;
    @NotNull
    public String password;

    public String category;
*/

    public PasswordEntry(String dominio,String username,String password,String category){
        this.dominio = dominio;
        this.username=username;
        this.password=password;
        this.category = category;
    }

    @Generated(hash = 932369869)
    public PasswordEntry(long id, String dominio, String username, String password,
            String category) {
        this.id = id;
        this.dominio = dominio;
        this.username = username;
        this.password = password;
        this.category = category;
    }

    @Generated(hash = 747656931)
    public PasswordEntry() {
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
    public String getCategory(){return this.category;}

    public void setDominio(String dominio){
        this.dominio = dominio;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public void setPassword(String password){ this.password = password; }
    public void setCategory(String category){this.category = category;}

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
