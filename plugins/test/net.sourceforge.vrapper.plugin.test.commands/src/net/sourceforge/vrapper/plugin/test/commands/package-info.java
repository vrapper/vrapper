/**
 * Following commands should work in all editors: :test-fixed-1, :test-fixed-2,
 * :test-volatile-hardcoded, :test-volatile-foo, :test-volatile-bar.
 * 
 * Editing file stuff/volatile-commands.properties should change available :test-volatile-...
 * commands without restarting.
 * 
 * Entering :test-ext-specific command in any file with extensions other than .ts or .js should
 * result in usual "Not an editor command: test-ext-specific" in status bar. In .ts or .js file this
 * command should display a message related to TypeScript or JavaScript correspondingly. Different
 * extensions can contribute commands with same name but with different behavior depnding on current
 * context.
 * 
 * Command :test-volatile-priority is implemented in three providers with different priorities.
 * Tweaking getPriority methods in Low/Higher/Highest...Provider while running Eclipse instance
 * under debugger should vary command behavior without restarting.
 * 
 * Volatile commands can choose (maybe depending on context) to override usual fixed commands,
 * including built-in ones. Command :test-abolish-reg overrides standard :reg command to do
 * something different. Entering :test-return-reg disables overriding, so usual :reg works again.
 */
package net.sourceforge.vrapper.plugin.test.commands;
