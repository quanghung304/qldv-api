package com.agribank.qldv_api.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class UserDetailsImpl implements UserDetails{
    private String id;
	private String dvCode;
    private Integer idIam;
	private String username;
	private String fullName;
	private String email;
	private Integer brcd;
	private Integer depId;
    private Collection<? extends GrantedAuthority> authorities;
    
//    public static UserDetailsImpl fromModel(User user){
//        List<GrantedAuthority> authorities = user.getQuyen().stream()
//            .map(role -> new SimpleGrantedAuthority(role.getName()))
//            .collect(Collectors.toList());
//        return new UserDetailsImpl(
//            user.getMaSo(),
//            user.getEmail(),
//            user.getEmail());
//    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
	public String getPassword() {
		return "";
	}

    @Override
	public String getUsername() {
		return username;
	}

    @Override
	public boolean isAccountNonExpired() {
		return true;
	}

    @Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UserDetailsImpl user = (UserDetailsImpl) o;
		return Objects.equals(id, user.id);
	}

}