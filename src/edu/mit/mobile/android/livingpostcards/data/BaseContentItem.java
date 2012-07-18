package edu.mit.mobile.android.livingpostcards.data;

import edu.mit.mobile.android.content.ContentItem;
import edu.mit.mobile.android.content.column.DBColumn;
import edu.mit.mobile.android.content.column.DatetimeColumn;
import edu.mit.mobile.android.content.column.TextColumn;

public interface BaseContentItem extends ContentItem {

	@DBColumn(type = DatetimeColumn.class, defaultValue = DatetimeColumn.NOW_IN_MILLISECONDS)
	public static final String CREATION_DATE = "creation_date";

	@DBColumn(type = DatetimeColumn.class, defaultValue = DatetimeColumn.NOW_IN_MILLISECONDS)
	public static final String MODIFICATION_DATE = "modification_date";

	@DBColumn(type = TextColumn.class)
	public static final String PUBLIC_URL = "public_url";


}
