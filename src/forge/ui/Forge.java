package forge.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.PrintStream;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import armory.Armory;
import forge.Undo;
import forge.input.DragHandler;
import forge.input.KeyboardHandler;
import forge.input.MouseHandler;
import forge.map.World;
import net.miginfocom.swing.MigLayout;
import ox.Config;
import ox.OS;
import swing.component.GFrame;
import swing.component.GPanel;
import swing.global.GFocus;

public class Forge extends GPanel {

  public static GFrame frame;
  public static boolean enableAnimations = true;
  public static boolean collisionMode = false;
  public static final int TILE_SIZE = 16;

  public final Armory armory = new Armory(this);
  public final World world = new World(armory);
  public final Canvas canvas = new Canvas(this);
  public final ToolPanel toolPanel = new ToolPanel(this);
  public final Undo undo = new Undo(this);

  public Forge() {
    super(new MigLayout("insets 0, gap 0"));

    canvas.setRegion(world.getRegion(Config.load("forge").getInt("last_region", 0)));

    add(toolPanel, "pos 0% 90% 100% 100%");
    add(armory, "pos 5% 0% 95% 90%");
    add(canvas, "width 100%, height 100%");

    setFocusable(true);

    new KeyboardHandler(this);
    new MouseHandler(this);
    new DragHandler(this);

    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(repaint, 0, 30, TimeUnit.MILLISECONDS);
  }

  private Runnable repaint = () -> {
    repaint();
  };

  private Runnable saveAndExit = () -> {
    Config.load("forge").put("last_region", canvas.region.id + "");

    world.save();
    armory.save();
    toolPanel.save();
    System.exit(0);
  };

  public static void main(String[] args) throws Exception {
    try {
      System.setProperty("apple.laf.useScreenMenuBar", "true");

      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      Forge forge = new Forge();
      frame = new GFrame("The Forge")
          .content(forge)
          .size(dim.width - 100, dim.height - 100)
          .disposeOnClose()
          .onClose(forge.saveAndExit)
          .start();
      new MenuOptions(forge);
      frame.setBackground(Color.black);
      GFocus.focus(forge);
    } catch (Exception e) {
      e.printStackTrace();
      e.printStackTrace(new PrintStream(new File(OS.getDesktop(), "forge_error.txt")));
      JOptionPane.showMessageDialog(null,
          "There was an error starting Forge. Check the forge_error.txt file on your desktop.");
    }
  }

}
