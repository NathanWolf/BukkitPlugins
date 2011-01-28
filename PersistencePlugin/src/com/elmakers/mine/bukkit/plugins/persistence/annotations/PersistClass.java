package com.elmakers.mine.bukkit.plugins.persistence.annotations;

public @interface PersistClass
{
	String schema();
	String name();
	boolean cache() default true;
}
