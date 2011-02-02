package com.elmakers.mine.bukkit.plugins.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PersistedList extends PersistedField
{
	public PersistedList(Field field)
	{
		super(field);
		findListType();
	}
	
	public PersistedList(Method getter, Method setter)
	{
		super(getter, setter);
		findListType();
	}
	
	public Class<?> getListType()
	{
		return listType;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getListValues(Object o)
	{
		// TODO : support contained lists.
		
		List<? extends Object> listItems = (List<? extends Object>)get(o);
		List<Object[]> valueList = new ArrayList<Object[]>();
		for (Object value : listItems)
		{
			if (referenceType == null)
			{
				valueList.add(new Object[] { value });
			}
			else
			{
				Object referenceId = referenceType.getId(value);
				valueList.add(new Object[] { referenceId } );
			}
		}
		
		return valueList;
	}
	
	@SuppressWarnings("unchecked")
	public List<Object> getListValueIds(Object o)
	{
		// TODO : support contained lists.
		
		List<? extends Object> listItems = (List<? extends Object>)get(o);
		List<Object> valueList = new ArrayList<Object>();
		for (Object value : listItems)
		{
			if (referenceType == null)
			{
				valueList.add(value);
			}
			else
			{
				Object referenceId = referenceType.getId(value);
				valueList.add(referenceId);
			}
		}
		
		return valueList;
	}
	
	public String getTableName(PersistedClass persisted)
	{
		String tableName = name;
		tableName = tableName.substring(0, 1).toUpperCase() + tableName.substring(1);
		return persisted.getTableName() + tableName;
	}
	
	public String getIdColumnName(PersistedClass persisted, PersistedField idField)
	{
		String tableName = persisted.getTableName();
		String[] idFieldNames = idField.getColumnNames();
		String idFieldName = tableName.substring(0, 1).toLowerCase() + tableName.substring(1) 
			+ idFieldNames[0].substring(0, 1).toUpperCase() + idFieldNames[0].substring(1);
		return idFieldName;
	}
	
	public int getDataColumnCount()
	{
		// TODO : support contained lists
		return 1;
	}
	
	public String getDataColumnName()
	{
		if (referenceType == null)
		{
			return getColumnName();
		}
		
		String referenceId = referenceType.getIdField().getColumnName();
		referenceId = referenceId.substring(0, 1).toUpperCase() + referenceId.substring(1);
		
		return getColumnName() + referenceId;
	}
	
	public DataType getListColumnType()
	{
		if (referenceType != null)
		{
			Class<?> referenceIdType = referenceType.getIdField().getType();
			return DataType.getTypeFromClass(referenceIdType);
		}
		return listDataType;
	}
	
	public DataType[] getListColumnTypes()
	{
		// TODO : support contained lists
		return new DataType[] { getListColumnType() };
	}
	
	public String[] getDataColumnNames()
	{
		// TODO : support contained lists
		return new String[] { getDataColumnName() };
	}
		
	@Override
	public String[] getColumnNames()
	{
		String columnName = name;
		if (columnName.charAt(columnName.length() - 1) == 's')
		{
			columnName = columnName.substring(0, columnName.length() - 1);
		}
		return new String[] { columnName };
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
	
	public void setList(Object instance, List<Object> items)
	{
		if (referenceType == null)
		{
			set(instance, items);
			return;
		}
		
		// Defer id lookup
		DeferredReferenceList list = deferListMap.get(instance);
		if (list == null)
		{
			list = new DeferredReferenceList(this);
			deferListMap.put(instance, list);
		}
		list.idList = items;
	}
	
	public static void endDefer()
	{
		deferStackDepth--;
		if (deferStackDepth > 0) return;
		
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

	protected Class<?> listType;
	protected DataType listDataType;
	protected PersistedClass referenceType = null;
	
}
