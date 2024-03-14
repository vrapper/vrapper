package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.RegisterManager;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

/**
 * Evaluator backing the <code>:let</code> command. Only limited expressions are available as
 * Vrapper prefers plugins written in Java.
 */
public class LetExpressionEvaluator implements Evaluator {
    public Object evaluate(EditorAdaptor vim, Queue<String> command) throws CommandExecutionException {
        if(command.isEmpty()) {
            vim.getUserInterfaceService().setErrorMessage("Argument required");
            return null;
        }
        String args = "";
        while(command.size() > 0)
            args += command.poll();

        if (args.startsWith("@")) {
            setRegisterContents(vim, args);
        } else if (args.toLowerCase(Locale.ENGLISH).matches("^mapleader[ =].*")) {
            setMapLeaderSetting(vim, args);
        } else {
            vim.getUserInterfaceService().setErrorMessage("Unsupported :let expression entered");
        }
        return null;
    }

    protected void setRegisterContents(EditorAdaptor vim, String args)
            throws CommandExecutionException {

        String[] expr = args.split("=", 2);
        if(expr.length != 2) {
            vim.getUserInterfaceService().setErrorMessage("Could not parse " + args);
            return;
        }
        if(expr[0].length() < 2) {
            vim.getUserInterfaceService().setErrorMessage("No register name given.");
            return;
        }

        RegisterManager registerManager = vim.getRegisterManager();

        String registerName = expr[0].substring(1, 2);
        String textContent = expr[1];
        if(expr[1].startsWith("'") && expr[1].endsWith("'") || 
               expr[1].startsWith("\"") && expr[1].endsWith("\"")) {
            textContent = expr[1].substring(1, expr[1].length() - 1);
        }
        else if(textContent.startsWith("@") && textContent.length() > 1) {
            textContent = registerManager.getRegister(textContent.substring(1, 2)).getContent().getText();
        }
        RegisterContent content = new StringRegisterContent(ContentType.TEXT, textContent);
        if (registerName.equals(RegisterManager.REGISTER_NAME_SEARCH)) {
            Search lastSearch = registerManager.getSearch();
            if (textContent.length() == 0) {
                registerManager.setSearch(null);
                HighlightSearch.CLEAR_HIGHLIGHT.evaluate(vim, new LinkedList<String>());
            } else {
                Search newSearch;
                if (lastSearch == null) {
                    newSearch = SearchCommandParser.createSearch(vim, textContent, false, null);
                } else {
                    newSearch = SearchCommandParser.createSearch(vim, textContent,
                            lastSearch.isBackward(), lastSearch.getSearchOffset());
                }
                registerManager.setSearch(newSearch);
                vim.setLastSearchResult(null);
                HighlightSearch.HIGHLIGHT.evaluate(vim, new LinkedList<String>());
            }
        } else {
            registerManager.getRegister(registerName).setContent(content, false);
        }
    }
    
    protected void setMapLeaderSetting(EditorAdaptor vim, String args) {

        String[] expr = args.split("=", 2);
        if(expr.length != 2) {
            vim.getUserInterfaceService().setErrorMessage("Could not parse " + args);
            return;
        }
        if(expr[0].length() < 2) {
            vim.getUserInterfaceService().setErrorMessage("No mapleader content given.");
            return;
        }

        RegisterManager registerManager = vim.getRegisterManager();

        String textContent = expr[1];
        if(expr[1].startsWith("'") && expr[1].endsWith("'") || 
               expr[1].startsWith("\"") && expr[1].endsWith("\"")) {
            textContent = expr[1].substring(1, expr[1].length() - 1);
        }
        else if(textContent.startsWith("@") && textContent.length() > 1) {
            textContent = registerManager.getRegister(textContent.substring(1, 2)).getContent().getText();
        }
        else {
            vim.getUserInterfaceService().setErrorMessage("Unknown expression value '" + expr[1]);
            return;
        }

        textContent = textContent.trim();
        vim.getConfiguration().set(Options.MAPLEADER, textContent);
    }
}