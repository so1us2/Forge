package armory;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JTabbedPane;

import com.google.common.base.Stopwatch;

import armory.rez.Resource;
import armory.rez.ResourcesPanel;
import forge.ui.Forge;
import ox.IO;
import ox.Json;
import ox.Log;
import ox.OS;
import swing.component.GPanel;

/**
 * Resource management
 */
public class Armory extends GPanel {

  private static final File dataDir, tilesetListing;
  private static int idCounter = 0;

  public final Forge forge;

  public final JTabbedPane tabs = new JTabbedPane();

  public final ResourcesPanel resourcesPanel;

  public Armory(Forge forge) {
    this.forge = forge;
    this.resourcesPanel = new ResourcesPanel(forge);

    add(tabs, "width 100%, height 100%");

    setVisible(false);

    tabs.addTab("Resources", resourcesPanel);
    load();
  }

  public Resource getResource(int id) {
    for (Resource rez : resourcesPanel.resources) {
      if (rez.getId() == id) {
        return rez;
      }
    }
    return null;
  }

  private void load() {
    Stopwatch watch = Stopwatch.createStarted();

    if (!tilesetListing.exists()) {
      return;
    }

    for (int id : IO.from(tilesetListing).toJson().asIntArray()) {
      idCounter = Math.max(idCounter, id + 1);
      TilesetPanel panel = TilesetPanel.load(this, dataDir, id);
      tabs.addTab(panel.id + 1 + "", panel);
    }

    Log.info("Armory loaded in " + watch);
  }

  public void save() {
    Stopwatch watch = Stopwatch.createStarted();

    Json tilesets = Json.array();
    for (int i = 0; i < tabs.getTabCount(); i++) {
      Component c = tabs.getComponentAt(i);
      if (c instanceof TilesetPanel) {
        TilesetPanel panel = (TilesetPanel) c;
        panel.save(dataDir);
        tilesets.add(panel.id);
      }
    }

    IO.from(tilesets).to(tilesetListing);

    Log.info("Armory saved in " + watch);
  }

  public void importTileset(String name, BufferedImage bi) {
    TilesetPanel panel = new TilesetPanel(this, idCounter++, name, bi);
    tabs.addTab(panel.id + 1 + "", panel);
    tabs.setSelectedIndex(tabs.getTabCount() - 1);
    setVisible(true);
  }

  static {
    dataDir = new File(OS.getAppFolder("forge"), "armory");
    tilesetListing = new File(dataDir, "tilesets.json");
    dataDir.mkdirs();
  }

}
