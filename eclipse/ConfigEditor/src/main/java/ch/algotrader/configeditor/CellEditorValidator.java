package ch.algotrader.configeditor;

import java.util.regex.Pattern;

import org.eclipse.jface.viewers.ICellEditorValidator;

import ch.algotrader.configeditor.editingSupport.CellEditorExtensionPoint;

public class CellEditorValidator implements ICellEditorValidator {

    private final EditorPropertyPage propertyPage;
    private final String dataType;

    public CellEditorValidator(EditorPropertyPage propertyPage, String dataType) {
        this.propertyPage = propertyPage;
        this.dataType = dataType;
    }

    @Override
    public String isValid(Object value) {
        String regex = CellEditorExtensionPoint.getRegex(dataType);
        if (regex == null)
            return null;
        if (Pattern.matches(regex, value.toString()))
            return null;
        return CellEditorExtensionPoint.getRegexErrorMessage(dataType, value.toString());
    }
}
