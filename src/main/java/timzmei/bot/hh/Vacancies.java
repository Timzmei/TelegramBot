package timzmei.bot.hh;

import lombok.Data;

@Data
public class Vacancies {

    private long id;
    private boolean premium;
    private String name;
    private Area area;
    private Salary salary;
    private String alternate_url;
    private Employer employer;
}
