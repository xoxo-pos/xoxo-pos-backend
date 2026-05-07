package com.xoxo.pos.users;
import com.fasterxml.jackson.annotation.JsonIgnore;import jakarta.persistence.*;
@Entity @Table(name="users") public class UserEntity{
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(unique=true,nullable=false) private String username; @JsonIgnore @Column(nullable=false) private String password; @Enumerated(EnumType.STRING) private Role role; private boolean active=true;
public UserEntity(){} public UserEntity(String u,String p,Role r,boolean a){username=u;password=p;role=r;active=a;}
public Long getId(){return id;} public String getUsername(){return username;} public String getPassword(){return password;} public Role getRole(){return role;} public boolean isActive(){return active;}
public void setId(Long id){this.id=id;} public void setUsername(String username){this.username=username;} public void setPassword(String password){this.password=password;} public void setRole(Role role){this.role=role;} public void setActive(boolean active){this.active=active;}}
