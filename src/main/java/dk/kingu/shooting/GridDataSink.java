package dk.kingu.shooting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.UI;

public class GridDataSink implements ResultWatchSink {

	ListDataProvider<ResultFile> listSink;
	UI ui;
	List<ResultFile> backingList;
	
	ResultWatch rw; 
	
	public GridDataSink(ResultWatch rw, UI ui) {
		this.ui = ui;
		this.rw = rw;
		backingList = new ArrayList<>();
		listSink = new ListDataProvider<>(backingList);
		rw.registerSink(this);
		update();
	}
	
	public ListDataProvider<ResultFile> getDataProvider() {
		return listSink;
	}
	
	@Override
	public void update() {
		List<ResultFile> res = new ArrayList<>(rw.getResults());
		Collections.sort(res, (ResultFile r1, ResultFile r2) -> r1.getResultDate().compareTo(r2.getResultDate()));
		Collections.reverse(res);
		backingList.clear();
		for(ResultFile f : res) {
			backingList.add(f);
		}
		
		ui.access(() -> listSink.refreshAll());
	}

}
