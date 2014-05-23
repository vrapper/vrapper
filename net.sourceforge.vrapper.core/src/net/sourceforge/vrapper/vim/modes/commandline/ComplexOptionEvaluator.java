package net.sourceforge.vrapper.vim.modes.commandline;

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
        try {
            if ((strOpt = find(Options.STRING_OPTIONS, optName)) != null) {
                if(additive) { //append ( += )
                    String current = vim.getConfiguration().get(strOpt);
                    set(vim, strOpt, current + value);
                }
                else if(subtractive) { //remove ( -= )
                    String current = vim.getConfiguration().get(strOpt);
                    set(vim, strOpt, current.replace(value, ""));
                }
                else {//normal set ( = )
                    set(vim, strOpt, value);
                }
            }
            else if ((intOpt = find(Options.INT_OPTIONS, optName)) != null) {
                set(vim, intOpt, Integer.valueOf(value));
            }
            else if ((boolOpt = find(Options.BOOLEAN_OPTIONS, optName)) != null) {
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

    protected <T> void set(EditorAdaptor adaptor, Option<T> opt, T value) throws ValueException {
        if (opt.getLegalValues() != null && !opt.getLegalValues().contains(value)) {
            throw new ValueException();
        }
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
