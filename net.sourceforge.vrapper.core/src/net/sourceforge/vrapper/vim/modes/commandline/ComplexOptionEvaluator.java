package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;

public class ComplexOptionEvaluator implements Evaluator {

    public Object evaluate(EditorAdaptor vim, Queue<String> command) {
        String next = command.poll();
        int index = next.indexOf('=');
        if (index < 0) {
            noSuchOptionMessage(vim, next);
            return null;
        }

        String optName = next.substring(0, index).trim();
        boolean additive = false;
        boolean subtractive = false;
        if(optName.endsWith("+")) {
            additive = true;
            optName = optName.substring(0, optName.length()-1);
        }
        else if(optName.endsWith("-")) {
            subtractive = true;
            optName = optName.substring(0, optName.length()-1);
        }

        String value = next.substring(index+1);
        if(command.size() > 0) {
        	//restore preceding space and spaces between tokens
        	value += " " + StringUtils.join(" ", command);
        }
        Option<String> strOpt;
        Option<Integer> intOpt;
        Option<Boolean> boolOpt;
        Option<Set<String>> stringSetOpt;
        try {
            if ((strOpt = find(Options.STRING_OPTIONS, optName)) != null) {
                if(additive) { //append ( += )
                    String newValue = vim.getConfiguration().get(strOpt) + value;
                    validate(strOpt, newValue);
                    set(vim, strOpt, newValue);
                }
                else if(subtractive) { //remove ( -= )
                    String newValue = vim.getConfiguration().get(strOpt).replace(value, "");
                    validate(strOpt, newValue);
                    set(vim, strOpt, newValue);
                }
                else {//normal set ( = )
                    validate(strOpt, value);
                    set(vim, strOpt, value);
                }
            }
            else if ((stringSetOpt = find(Options.STRINGSET_OPTIONS, optName)) != null) {
                Set<String> newValue = new HashSet<String>();
                if (value.trim().length() > 0) {
                    String[] values = value.split(Option.SET_DELIMITER);
                    newValue.addAll(Arrays.asList(values));
                    validateSet(stringSetOpt, newValue);
                }
                set(vim, stringSetOpt, newValue);
            }
            else if ((intOpt = find(Options.INT_OPTIONS, optName)) != null) {
                validate(intOpt, Integer.valueOf(value));
                set(vim, intOpt, Integer.valueOf(value));
            }
            else if ((boolOpt = find(Options.BOOLEAN_OPTIONS, optName)) != null) {
                validate(boolOpt, Boolean.valueOf(value));
                set(vim, boolOpt, Boolean.valueOf(value));
            }
            else {
                noSuchOptionMessage(vim, optName);
            }
        } catch (ValueException e) {
            invalidValueMessage(vim, value);
        } catch (NumberFormatException e) {
            invalidValueMessage(vim, value);
        }
        return null;
    }

    private void invalidValueMessage(EditorAdaptor vim, String value) {
        vim.getUserInterfaceService().setErrorMessage("Invalid value: " + value);
    }

    private void noSuchOptionMessage(EditorAdaptor vim, String name) {
        vim.getUserInterfaceService().setErrorMessage("Unknown option: " + name);
    }
    
    protected <T> void validate(Option<T> opt, T value) throws ValueException {
        if (opt.getLegalValues() != null && !opt.getLegalValues().contains(value)) {
            throw new ValueException();
        }
    }

    protected void validateSet(Option<Set<String>> opt, Set<String> values) throws ValueException {
        Set<String> cleanedValues = new HashSet<String>();
        for (String value : values) {
            int valueDelimIndex = value.indexOf(Option.SET_VALUE_ITEM);
            if (valueDelimIndex != -1) {
                cleanedValues.add(value.substring(0, valueDelimIndex + 1));
            } else {
                cleanedValues.add(value);
            }
        }
        if (opt.getLegalValues() == null || !opt.getLegalValues().containsAll(cleanedValues)) {
            throw new ValueException();
        }
    }

    protected <T> void set(EditorAdaptor adaptor, Option<T> opt, T value) throws ValueException {
        adaptor.getConfiguration().set(opt, value);
    }

    private <T> Option<T> find(Set<Option<T>> options, String optName) {
        for (Option<T> o : options) {
            for (String name : o.getAllNames()) {
                if (name.equals(optName)) {
                    return o;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("serial")
    class ValueException extends Exception { /* private marker exception */ }

}
