package com.simon.credit.toolkit;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.simon.credit.toolkit.common.CommonToolkits;
import com.simon.credit.toolkit.io.IOToolkits;
import com.simon.credit.toolkit.io.LineIterator;

public class ReadFileTest {

	public static void main(String[] args) throws IOException {
		LineIterator lineIterator = IOToolkits.lineIterator(new File("d:/insert.txt"));

		List<String> infos = IOToolkits.readLines(new File("d:/infos.txt"));

		while (lineIterator.hasNext()) {
			String line = lineIterator.nextLine();
			boolean exists = false;
			for (int i = 0; i < infos.size(); i++) {
				String[] ary = infos.get(i).split("$");
				if (CommonToolkits.contains(line, ary[1])) {
					exists = true;
					String id = ary[0];
					String cityZoneCode = line.substring(line.length() - 9, line.length() - 3);
					String sql = "UPDATE tb_sys_cityzone set CITYZONECODE='" + cityZoneCode + "' WHERE CITYZONEID=" + id + "; -- " + ary[1];
					System.out.println(sql);
					break;
				}
			}

			if (!exists) {
				// System.out.println(line);
			}
		}

	}

}
