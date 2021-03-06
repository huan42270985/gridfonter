package pl.edu.pw.mini.jozwickij.ttfedit.tables.common.objects;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import pl.edu.pw.mini.jozwickij.ttfedit.DefaultProperties;
import pl.edu.pw.mini.jozwickij.ttfedit.gui.MapAllLabel;
import pl.edu.pw.mini.jozwickij.ttfedit.gui.MapLabel;
import pl.edu.pw.mini.jozwickij.ttfedit.table.TTFTable;
import pl.edu.pw.mini.jozwickij.ttfedit.table.TTFTables;
import pl.edu.pw.mini.jozwickij.ttfedit.tables.common.TTFTable_OS_2;

public class TTFTable_cmapFormat {
	public int format = 0;
	private byte[] body = null;
	protected int my_offset = 0;
	protected int my_length = 0;
	public Map<String, TTFTable> ttfTables = null;
	
	protected final static int GID_POOL = 8;
		
	public TTFTable_cmapFormat() {}
	
	protected void init(RandomAccessFile ttf, int offset, int length) throws IOException {
		this.my_offset = offset;
		this.my_length = length;
		ttf.seek(offset);
	}
	
	public TTFTable_cmapFormat(RandomAccessFile ttf, int offset, int length, Map<String, TTFTable> tables) throws IOException {
		init(ttf,offset,length);
		format = ttf.readUnsignedShort();
		body = new byte[length > 0 ? length-2 : 0];
		ttf.readFully(body);
		this.ttfTables = tables;
		ttf.seek(offset);		
	}
	
	public int getGlyphIdForChar(int ch) { return 0; }
	
	public Component getView(Object obj, Font f) {		
		JPanel view = new JPanel();
		view.setLayout(new GridLayout(0,1));
		String info = this.getInfo();
		if (obj instanceof String)
			info += " (" + obj + ")";
		view.add(new JLabel("<html><b>"+info+"</b></html>"));
		view.setBorder(new EmptyBorder(DefaultProperties.SMALL_PAD,
									   DefaultProperties.SMALL_PAD,
									   DefaultProperties.SMALL_PAD,
									   DefaultProperties.SMALL_PAD));
		/*Filtering UTF-16/32 and 8-bit ASCII CMAPs*/
		if ((this.format > 8.0 && this.format < 12.0)|| this.format < 4) {			
			return view;			
		}
		boolean addMapAll = false;
		for (TTFTable_cmapMap map : TTFTable_cmapPL.getCmap()) {
			view.add(new MapLabel(f,map,this));
			if (!addMapAll && this.getGlyphIdForChar(map.ch)==0)
				addMapAll = true;
		}
		if (addMapAll) {
			view.add(new MapAllLabel(f,TTFTable_cmapPL.getCmap(),this, view));
		}
		return view;
	}
	
	public int write(RandomAccessFile ttf, int off) throws IOException {
		ttf.seek(off);
		ttf.writeShort(format);
		ttf.write(body);
		return (int)(ttf.getFilePointer()-off);
	}
	
	public String getInfo() { 
		return "Unknown/not supported CMAP format "+this.format;
	}
	
	protected void prepareWrite(RandomAccessFile ttf, int off) throws IOException {
		ttf.seek(off);
	}
	
	protected int finishWrite(RandomAccessFile ttf, int off) throws IOException {
		return (int)(ttf.getFilePointer()-off);
	}
	
	public void injectGlyphMapping(int charID, int glyphID) throws Exception {}
	
	protected void fixOS2(int charID) {
		TTFTable_OS_2 os2 = (TTFTable_OS_2) ttfTables.get(TTFTables.OS_2);
		if (os2!=null) {
			if (os2.fsFirstCharIndex > charID) {
				os2.fsFirstCharIndex = charID;
			}
			else if (os2.fsLastCharIndex < charID) {
				os2.fsLastCharIndex = charID;
			}
		}
	}
}
