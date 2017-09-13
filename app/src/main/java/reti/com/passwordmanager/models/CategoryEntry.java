package reti.com.passwordmanager.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
@Keep
public class CategoryEntry {
    @NotNull
    public String category;

    public CategoryEntry(String category){
        this.category = category;
    }

    public void setCategory(String category){
        this.category = category;
    }

    public String getCategory(){
        return this.category;
    }
}
