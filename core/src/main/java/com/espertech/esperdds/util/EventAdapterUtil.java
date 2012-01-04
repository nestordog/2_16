// outcomment fragment information

package com.espertech.esperdds.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.espertech.esper.client.EventPropertyDescriptor;
import com.espertech.esper.client.FragmentEventType;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.event.EventAdapterServiceImpl;
import com.espertech.esper.event.EventTypeIdGeneratorImpl;
import com.espertech.esper.event.EventTypeSPI;
import com.espertech.esper.servicexml.Property;
import com.espertech.esperdds.renderer.ByteSerializer;
import com.espertech.esperdds.renderer.ByteSerializerFactory;

public class EventAdapterUtil {

    public EventAdapterUtil() {
    }

    public static EventAdapterService getEventAdapterService() {
        return eventAdapterServiceImpl;
    }

    public static Property[] getProperties(Class clazz, boolean displayWriteCopy) {
        EventTypeSPI type = (EventTypeSPI) eventAdapterServiceImpl.addBeanType(clazz.getName(), clazz, false, false, false);
        return getProperties(type, displayWriteCopy);
    }

    public static Property[] getProperties(EventTypeSPI type, boolean displayWriteCopy) {
        List coll = new ArrayList();
        EventPropertyDescriptor arr$[] = type.getPropertyDescriptors();
        int len$ = arr$.length;
        for (int i$ = 0; i$ < len$; i$++) {
            EventPropertyDescriptor prop = arr$[i$];
            String fragmentTypeName = null;
            Boolean fragmentIsNative = null;
            Boolean fragmentIsIndexed = null;
            if (prop.isFragment()) {
                FragmentEventType fragmentType = type.getFragmentType(prop.getPropertyName());
                if (fragmentType == null) {
                    log.warn((new StringBuilder()).append("Failed to obtain fragment type for property '").append(prop.getPropertyName()).append("' of type '").append(type.getName()).append("'")
                            .toString());
                } else {
                    fragmentTypeName = fragmentType.getFragmentType().getName();
                    fragmentIsNative = Boolean.valueOf(fragmentType.isNative());
                    fragmentIsIndexed = Boolean.valueOf(fragmentType.isIndexed());
                }
            }
            Property desc = com.espertech.esper.servicexml.Property.Factory.newInstance();
            desc.setName(prop.getPropertyName());
            if (displayWriteCopy)
                desc.setWritable(type.getWritableProperty(prop.getPropertyName()) != null);
            if (prop.getPropertyType() == null)
                desc.setType("null");
            else
                desc.setType(prop.getPropertyType().getName());
            if (prop.getPropertyComponentType() != null)
                desc.setComponentType(prop.getPropertyComponentType().getName());
            ByteSerializer serializer;
            if (fragmentTypeName == null) {
                if (prop.getPropertyComponentType() != null)
                    serializer = ByteSerializerFactory.getSerializer(prop.getPropertyComponentType());
                else
                    serializer = ByteSerializerFactory.getSerializer(prop.getPropertyType());
            } else {
                serializer = ByteSerializerFactory.getSerializer(prop.getPropertyComponentType());
            }
            if (serializer != null)
                desc.setSerializer(serializer.getClass().getSimpleName());
            desc.setRequiresIndex(prop.isRequiresIndex());
            desc.setRequiresMapKey(prop.isRequiresMapkey());
            desc.setIndexed(prop.isIndexed());
            desc.setMapped(prop.isMapped());
            if (fragmentTypeName != null) {
                desc.setFragmentTypeName(fragmentTypeName);
                desc.setFragmentIsIndexed(fragmentIsIndexed.booleanValue());
                desc.setFragmentIsNative(fragmentIsNative.booleanValue());
                FragmentEventType fragment = type.getFragmentType(desc.getName());
                // if (fragment != null) {
                // EventTypeSPI fragmentType = (EventTypeSPI)
                // fragment.getFragmentType();
                // Property fragmentProperties[] = getProperties(fragmentType,
                // displayWriteCopy);
                // desc.setFragmentPropArray(fragmentProperties);
                // }
            }
            coll.add(desc);
        }

        return (Property[]) coll.toArray(new Property[coll.size()]);
    }

    private static Log log = LogFactory.getLog(EventAdapterUtil.class);
    private static final EventAdapterService eventAdapterServiceImpl = new EventAdapterServiceImpl(new EventTypeIdGeneratorImpl());

}
