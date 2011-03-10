
package com.elmakers.mine.craftbukkit.permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a definition of permissions for a plugin
 */
public class RootPermissionDescription extends MapPermissionDescriptionNode {
    private final Map<String, PermissionDescriptionNode> permissions = new HashMap<String, PermissionDescriptionNode>();

    @SuppressWarnings("LeakingThisInConstructor")
    public RootPermissionDescription(final Map<String, Object> map)
            throws PermissionDescriptionException, PermissionDescriptionNodeException {
        super(map);

        try {
            loadMap(map);
        } catch (ClassCastException ex) {
            throw new PermissionDescriptionNodeException("Root permissions are not maps", ex);
        }
    }
    
    public boolean isDefaultSet(final String path)
    {
    	String[] keys = path.split("\\.");
        MapPermissionDescriptionNode top = this;

        for (int i = 0; i < keys.length; i++) {
            PermissionDescriptionNode node = top.getNode(keys[i]);
            if (node == null) continue;

            if (node instanceof MapPermissionDescriptionNode) {
            	top = (MapPermissionDescriptionNode)node;
            } else if (node instanceof ListPermissionDescriptionNode && i == keys.length - 1) {
            	ListPermissionDescriptionNode list = (ListPermissionDescriptionNode)node;
            	
            	@SuppressWarnings("unchecked")
				ArrayList<Object> arrayList = (ArrayList<Object>)list.getDefault();
            	if (arrayList != null) {
            		return arrayList.contains(keys[i]);
            	}
            } else if (node instanceof BooleanPermissionDescriptionNode && i == keys.length - 1) {
                return (Boolean)node.getDefault();
            }   
            else {
            	return false;
            }
        }

        return false;
    }

    public PermissionDescriptionNode getPath(final String map) {
        String[] keys = map.split("\\.");
        MapPermissionDescriptionNode top = this;

        for (int i = 0; i < keys.length - 1; i++) {
            PermissionDescriptionNode node = top.getNode(keys[i]);
            if (node == null) continue;

            if (!(node instanceof MapPermissionDescriptionNode)) {
                StringBuilder builder = new StringBuilder();

                for (int j = 0; j <= i; j++) {
                    if (builder.length() > 0) {
                        builder.append('.');
                    }
                    builder.append(keys[j]);
                }

                throw new IllegalArgumentException(builder.toString() + " is not a map");
            }

            top = (MapPermissionDescriptionNode)node;
        }

        return top.getNode(keys[keys.length - 1]);
    }
}