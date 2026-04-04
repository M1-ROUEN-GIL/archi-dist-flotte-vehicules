package com.flotte.maintenance.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mappe les rôles Keycloak vers des {@code GrantedAuthority} {@code ROLE_*}.
 * Prend en charge {@code realm_access.roles} et les rôles clients dans {@code resource_access.*.roles}
 * (utile si le jeton est « léger » ou si seuls les rôles client sont présents).
 */
public final class KeycloakJwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

	@Override
	public Collection<GrantedAuthority> convert(Jwt jwt) {
		Set<String> roles = new LinkedHashSet<>();
		Map<String, Object> realmAccess = jwt.getClaim("realm_access");
		if (realmAccess != null) {
			addRoles(realmAccess.get("roles"), roles);
		}
		Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
		if (resourceAccess != null) {
			for (Object clientEntry : resourceAccess.values()) {
				if (clientEntry instanceof Map<?, ?> m) {
					Object r = m.get("roles");
					addRoles(r, roles);
				}
			}
		}
		return roles.stream()
			.map(r -> new SimpleGrantedAuthority("ROLE_" + r))
			.collect(Collectors.toList());
	}

	private static void addRoles(Object raw, Set<String> out) {
		if (!(raw instanceof Collection<?> c)) {
			return;
		}
		for (Object o : c) {
			if (o != null && !o.toString().isBlank()) {
				out.add(o.toString());
			}
		}
	}
}
