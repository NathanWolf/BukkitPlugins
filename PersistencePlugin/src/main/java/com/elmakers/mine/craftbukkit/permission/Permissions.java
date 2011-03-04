/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elmakers.mine.craftbukkit.permission;

/**
 * Represents a structure which may contain permissions, such as a profile or a user
 */
public interface Permissions {
    <T> T get(final String key);

    boolean isSet(final String key);

    void set(final String key, final Object value);
}
