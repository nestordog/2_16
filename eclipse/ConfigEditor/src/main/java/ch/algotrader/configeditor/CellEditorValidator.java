package ch.algotrader.configeditor;

import java.util.regex.Pattern;

import org.eclipse.jface.viewers.ICellEditorValidator;

import ch.algotrader.configeditor.editingSupport.CellEditorExtensionPoint;

public class CellEditorValidator implements ICellEditorValidator {

    private final EditorPropertyPage propertyPage;
    private final String dataType;
    private final String key;

    public CellEditorValidator(EditorPropertyPage propertyPage, String dataType, String pKey) {
        this.propertyPage = propertyPage;
        this.dataType = dataType;
        key = pKey;
    }

    @Override
    public String isValid(Object value) {
        FieldModel model = new FieldModel(propertyPage.getSelectedProperties().getValueStruct(key));
        if (model.getRequired() && (value == null || value.toString().equals(""))) {
            String label = model.getLabel();
            if (label == null)
                label = key;
            return "The property \"" + label + "\" is required";
        }
        String regex = CellEditorExtensionPoint.getRegex(dataType);
        if (regex == null)
            return null;
        if (Pattern.matches(regex, value.toString()))
            return null;
        return CellEditorExtensionPoint.getRegexErrorMessage(dataType, value.toString());
    }
}
