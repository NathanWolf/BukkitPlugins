package com.elmakers.mine.bukkit.plugins.persistence.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.data.DataField;
import com.elmakers.mine.bukkit.plugins.persistence.data.DataRow;
import com.elmakers.mine.bukkit.plugins.persistence.data.DataTable;
import com.elmakers.mine.bukkit.plugins.persistence.data.DataType;

/**
 * A variant of PersistedField that handles persisting Lists
 * 
 * The class tries to abstract some of the complexity of persisting Lists of data,
 * including creating and using sub-tables.
 * 
 * It also supports Lists of contained objects, storing object data directly
 * in the list sub-table.
 * 
 * @author nathan
 *
 */
public class PersistedList extends PersistedField
{
	public PersistedList(Field field, PersistedClass owningClass)
	{
		super(field);
		owningType = owningClass;
		findListType();
	}
	
	public PersistedList(Method getter, Method setter, PersistedClass owningClass)
	{
		super(getter, setter);
		owningType = owningClass;
		findListType();
	}
	
	public void load(DataTable subTable, List<Object> instances)
	{
		// TODO: support contained lists
		
		// Load data for all lists in all instances at once, mapping to
		// correct instances based on the id column.
		
		HashMap<Object, Object> objectIdMap = new HashMap<Object, Object>();
		HashMap<Object, List<Object> > objectLists = new HashMap<Object, List<Object> >();
		for (Object instance : instances)
		{
			Object instanceId = owningType.getId(instance);
			objectIdMap.put(instanceId, instance);
			List<Object> listData = new ArrayList<Object>();
			objectLists.put(instanceId, listData);
		}
		
		PersistedField idField = owningType.getIdField();
		String idName = idField.getDataName();
		
		for (DataRow row : subTable.getRows())
		{
			Object id = row.get(idName);
			Object data = row.get(getDataName());
			List<Object> list = objectLists.get(id);
			if (list != null);
			{
				list.add(data);
			}
		}
		
		// Assign lists to instance fields, or defer until later
		for (Object objectId : objectLists.keySet())
		{
			List<Object> listData = objectLists.get(objectId);
			Object instance = objectIdMap.get(objectId);
			
			if (referenceType == null)
			{
				set(instance, listData);
			}
			else
			{
				DeferredReferenceList list = deferListMap.get(instance);
				if (list == null)
				{
					list = new DeferredReferenceList(this);
					deferListMap.put(instance, list);
				}
				list.idList = listData;
			}
		}
	}
	
	protected void populate(DataRow dataRow, Object data)
	{
		PersistedField idField = owningType.getIdField();
		
		// Add id row first, this binds to the owning class
		DataField idData = new DataField(idField.getDataName(), idField.getDataType());
		idData.setIdField(true);
		dataRow.add(idData);
		
		// TODO : support contained objects
				
		// Add data rows
		if (referenceType == null)
		{
			DataField valueData = new DataField(getDataName(), listDataType);
			valueData.setIdField(true);
			if (data != null)
			{
				valueData.setValue(data);
			}
			dataRow.add(valueData);
		}
		else
		{
			PersistedField referenceIdField = referenceType.getIdField();
			
			// Construct a field name using the name of the reference id
			String referenceFieldName = referenceIdField.getDataName();
			referenceFieldName = referenceFieldName.substring(0, 1).toUpperCase() + referenceFieldName.substring(1);
			referenceFieldName = getDataName() + referenceFieldName;
			
			DataField referenceIdData = new DataField(referenceFieldName, referenceIdField.getDataType());
			if (data != null)
			{
				Object id = referenceIdField.get(data);
				referenceIdData.setValue(id);
			}
			referenceIdData.setIdField(true);
			dataRow.add(referenceIdData);
		}	
	}
	
	public void populateHeader(DataTable dataTable)
	{
		dataTable.createHeader();
		DataRow headerRow = dataTable.getHeader();
		populate(headerRow, null);
	}
	
	public void save(DataTable table, Object instance)
	{
		if (instance == null) return;
		
		@SuppressWarnings("unchecked")
		List<? extends Object> list = (List<? extends Object>)get(instance);
		for (Object data : list)
		{
			DataRow row = new DataRow(table);
			populate(row, data);
			table.addRow(row);
		}
	}
	
	protected void findListType()
	{
        Type type = getGenericType();  
        
        if (type instanceof ParameterizedType) 
        {  
            ParameterizedType pt = (ParameterizedType)type;
            if (pt.getActualTypeArguments().length > 0)
            {
            	listType = (Class<?>)pt.getActualTypeArguments()[0];
            }
        }
        listDataType = DataType.getTypeFromClass(listType);
        
        // Construct sub-table name
		tableName = name.substring(0, 1).toUpperCase() + name.substring(1);
		tableName = owningType.getTableName() + tableName;
	}
	
	public Class<?> getListType()
	{
		return listType;
	}
	
	public String getTableName()
	{
		return tableName;
	}
	
	protected Type getGenericType()
	{
		Type genericType = null;
		if (getter != null)
		{
			genericType = getter.getGenericReturnType();
		}
		else
		{
			genericType = field.getGenericType();
		}
		return genericType;
	}
	
	@Override
	public void bind()
	{
        if (listDataType == DataType.OBJECT)
        {
        	referenceType = Persistence.getInstance().getPersistedClass(listType);
        }
	}
	
	public static void beginDefer()
	{
		deferStackDepth++;
	}
	
	public static void endDefer()
	{
		deferStackDepth--;
		if (deferStackDepth > 0) return;
		
		// TODO: Handle contained objects
		
		for (Object instance : deferListMap.keySet())
		{
			List<Object> references = new ArrayList<Object>();
			DeferredReferenceList ref = deferListMap.get(instance);
			for (Object id : ref.idList)
			{
				if (id == null) 
				{
					references.add(null);
				}
				else
				{
					Object reference = ref.referenceList.referenceType.get(id);
					references.add(reference);
				}
			}
			
			ref.referenceList.set(instance, references);
		}
		deferListMap.clear();
	}
	
	class DeferredReferenceList
	{
		public PersistedList referenceList;
		public List<Object> idList;
		
		public DeferredReferenceList(PersistedList listField)
		{
			referenceList = listField;
		}
	}

	private static int deferStackDepth = 0;
	private final static HashMap<Object, DeferredReferenceList> deferListMap = new HashMap<Object, DeferredReferenceList>();

	protected final PersistedClass owningType;
	protected String tableName;
	protected Class<?> listType;
	protected DataType listDataType;

	// Only valid for Lists of Objects
	protected PersistedClass referenceType = null;
}
