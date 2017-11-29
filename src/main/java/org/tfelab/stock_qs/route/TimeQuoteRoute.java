package org.tfelab.stock_qs.route;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tfelab.db.OrmLiteDaoManager;
import org.tfelab.io.server.Msg;
import org.tfelab.io.server.MsgTransformer;
import org.tfelab.stock_qs.cache.Cache;
import org.tfelab.stock_qs.model.TimeQuote;
import org.tfelab.stock_qs.util.PandasData;
import org.tfelab.txt.DateFormatUtil;

import java.util.Date;
import java.util.List;

import static spark.Spark.get;

public class TimeQuoteRoute {

	public static final Logger logger = LogManager.getLogger(TimeQuoteRoute.class.getName());

	public TimeQuoteRoute() {

		get("/stock/time_quotes", (req, res) -> {

			try {

				String code = req.queryParams("code");
				String sd = req.queryParams("sd");
				String ed = req.queryParams("ed");
				String type = req.queryParams("type");

				Date sd_ = DateFormatUtil.parseTime(sd);
				Date ed_ = DateFormatUtil.parseTime(ed);

				if(ed_.getTime() <= sd_.getTime()) {
					throw new Exception("查询结束时间小于开始时间");
				}

				if(ed_.getTime() > sd_.getTime() + 86400 * 1000 * 7) {
					throw new Exception("查询时间范围应小于7天");
				}

				if(!type.equals("pandas_df_json")) {
					throw new Exception("type参数不正确");
				}

				if(!Cache.codes.contains(code)) {
					throw new Exception("股票代码不正确");
				}

				Dao<TimeQuote, String> dao = OrmLiteDaoManager.getDao(TimeQuote.class);

				QueryBuilder<TimeQuote, String> queryBuilder = dao.queryBuilder();

				List<TimeQuote> list = queryBuilder.where().eq("code", code).and().ge("time", sd_).and().le("time", ed_).query();

				return new PandasData.TimeQuotes(list);

			} catch (Exception e) {
				logger.error("Error query transactions.", e);
				return new Msg<>(Msg.FAILURE, e.getMessage());
			}

		}, new MsgTransformer());

	}

}
