package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;


public enum ConfigCommand implements Command {

    GLOBAL_REGISTERS {
        public void execute(EditorAdaptor vim) {
            vim.useGlobalRegisters();
        }
    },
    LOCAL_REGISTERS {
        public void execute(EditorAdaptor vim) {
            vim.useLocalRegisters();
        }
    },
    AUTO_INDENT {
        public void execute(EditorAdaptor vim) {
            vim.getConfiguration().setAutoIndent(true);
        }
    },
    NO_AUTO_INDENT {
        public void execute(EditorAdaptor vim) {
            vim.getConfiguration().setAutoIndent(false);
        }
    },
    LINE_WISE_MOUSE_SELECTION {
        public void execute(EditorAdaptor vim) {
            // FIXME: implement or remove
//            vim.getPlatform().setLineWiseMouseSelection(true);
        }
    },
    NO_LINE_WISE_MOUSE_SELECTION {
        public void execute(EditorAdaptor vim) {
            // FIXME: implement or remove
//            vim.getPlatform().setLineWiseMouseSelection(false);
        }
    },
    START_OF_LINE {
        public void execute(EditorAdaptor vim) {
            vim.getConfiguration().setStartOfLine(true);
        }
    },
    NO_START_OF_LINE {
        public void execute(EditorAdaptor vim) {
            vim.getConfiguration().setStartOfLine(false);
        }
    },
    SMART_INDENT {
        public void execute(EditorAdaptor vim) {
            vim.getConfiguration().setSmartIndent(true);
        }
    },
    NO_SMART_INDENT{
        public void execute(EditorAdaptor vim) {
            vim.getConfiguration().setSmartIndent(false);
        }
    };

    public int getCount() {
        return NO_COUNT_GIVEN;
    }

    public Command repetition() {
        return null;
    }

    public Command withCount(int count) {
        return this;
    }

}
