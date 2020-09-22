package com.pelgray;

import java.util.ArrayList;
import java.util.List;

public class Vacancy {
    /**
     * Класс-идентификатор вакансии (поле со ссылкой на вакансию)
     */
    @SheetColumn(name = "Идентификатор")
    Identifier identifier = new Identifier();

    /**
     * Идентификатор вакансии
     */
    String id;

    /**
     * Название вакансии
     */
    @SheetColumn(name = "Название")
    String name;

    /**
     * Регион размещения вакансии
     */
    Area area;

    /**
     * Специализации
     */
    @SheetColumn(name = "Специализации")
    List<Specialization> specialization;

    /**
     * Ключевые навыки
     */
    @SheetColumn(name = "Ключевые навыки")
    List<KeySkill> key_skills;

    /**
     * График работы
     */
    @SheetColumn(name = "График работы")
    Schedule schedule;

    /**
     * Требуемый опыт работы
     */
    @SheetColumn(name = "Требуемый опыт")
    Experience experience;

    /**
     * Адрес вакансии
     */
    @SheetColumn(name = "Адрес")
    Address address;

    /**
     * Тип занятости
     */
    @SheetColumn(name = "Тип занятости")
    Employment employment;

    /**
     * Оклад
     */
    @SheetColumn(name = "Оклад")
    Salary salary;

    /**
     * Короткое представление работодателя
     */
    Employer employer;

    /**
     * В архиве
     */
    @SheetColumn(name = "В архиве")
    boolean archived;

    /**
     * Информация о наличии прикрепленного тестового задании к вакансии. В случае присутствия теста - true.
     */
    boolean has_test;

    /**
     * Информация о прикрепленном тестовом задании к вакансии. В случае отсутствия теста — null.
     */
    Test test;

    /**
     * Ссылка на представление вакансии на сайте
     */
    String alternate_url;

    /**
     * Дата и время создания вакансии
     */
    String created_at;

    /**
     * Дата и время публикации вакансии
     */
    String published_at;


    /**
     * Идентификатор вакансии со ссылкой на нее
     */
    public class Identifier {
        @Override
        public String toString() {
            return String.format("=ГИПЕРССЫЛКА(\"%s\";\"%s\")", Vacancy.this.alternate_url, Vacancy.this.id);
        }
    }

    /**
     * Оклад
     */
    public class Salary {
        /**
         * Нижняя граница вилки оклада
         */
        int from;
        /**
         * Верняя граница вилки оклада
         */
        int to;
        /**
         * Признак того что оклад указан до вычета налогов. В случае если не указано - null.
         */
        boolean gross;
        /**
         * Идентификатор валюты оклада
         */
        String currency;

        @Override
        public String toString() {
            if (from == 0 && to == 0)
                return "Не указана";
            String res = "";
            if (from != 0)
                res += "от " + from / 1000 + "к";
            if (to != 0)
                res += " до " + to / 1000 + "к";
            res += String.format(" %s", gross ? "ставка" : "на руки");
            return res;
        }
    }

    /**
     * Адрес вакансии
     */
    public class Address {
        /**
         * Город
         */
        String city;
        /**
         * Улица
         */
        String street;
        /**
         * Номер дома
         */
        String building;
        /**
         * Дополнительная информация об адресе
         */
        String description;
        /**
         * Географическая широта
         */
        double lat;
        /**
         * Географическая долгота
         */
        double lng;
        /**
         * Список станций метро, может быть пустым
         */
        List<MetroStation> metro_stations;

        @Override
        public String toString() {
            String ret = "";
            if (city != null) {
                ret += city;
            }
            if (street != null) {
                if (!ret.equals(""))
                    ret += ", ";
                ret += street;
            }
            if (building != null) {
                if (!ret.equals(""))
                    ret += ", ";
                ret += building;
            }
            if (metro_stations != null && !metro_stations.isEmpty()) {
                if (!ret.equals(""))
                    ret += "; метро: ";
                ret += metro_stations.toString().substring(1, metro_stations.toString().length() - 1);
            }
            if (description != null) {
                if (!ret.equals(""))
                    ret += "\nОписание: ";
                ret += description;
            }
            return ret;
        }
    }

    /**
     * Станция метро
     */
    public class MetroStation {
        /**
         * Идентификатор станции метро
         */
        String station_id;
        /**
         * Название станции метро
         */
        String station_name;
        /**
         * Идентификатор линии метро, на которой находится станция
         */
        String line_id;
        /**
         * Название линии метро, на которой находится станция
         */
        String line_name;
        /**
         * Географическая широта станции метро
         */
        double lat;
        /**
         * Географическая долгота станции метро
         */
        double lng;

        @Override
        public String toString() {
            return station_name;
        }
    }

    /**
     * Требуемый опыт работы
     */
    public class Experience {
        /**
         * Идентификатор требуемого опыта работы
         */
        String id;
        /**
         * Название требуемого опыта работы
         */
        String name;

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * График работы
     */
    public class Schedule {
        /**
         * Идентификатор графика работы
         */
        String id;
        /**
         * Название графика работы
         */
        String name;

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Тип занятости
     */
    public class Employment {
        /**
         * Идентификатор типа занятости
         */
        String id;
        /**
         * Название типа занятости
         */
        String name;

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Специализации
     */
    public class Specialization {
        /**
         * Идентификатор специализации
         */
        String id;
        /**
         * Название специализации
         */
        String name;
        /**
         * Идентификатор профессиональной области, в которую входит специализация
         */
        String profarea_id;
        /**
         * Название профессиональной области, в которую входит специализация
         */
        String profarea_name;

        @Override
        public String toString() {
            return name + " (" + profarea_name + ")";
        }
    }

    /**
     * Ключевой навык
     */
    public class KeySkill {
        /**
         * Название ключевого навыка
         */
        String name;

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Регион размещения вакансии
     */
    public class Area {
        /**
         * Название региона
         */
        String name;

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Короткое представление работодателя
     */
    public class Employer {
        /**
         * Название работодателя
         */
        String name;
        /**
         * URL для получения полного описания работодателя
         */
        String url;

        @Override
        public String toString() {
            return String.format("=ГИПЕРССЫЛКА(\"%s\";\"%s\")", url, name);
        }
    }

    /**
     * Информация о прикрепленном тестовом задании к вакансии
     */
    public class Test {
        /**
         * Обязательно ли заполнение теста для отклика
         */
        boolean required;

        @Override
        public String toString() {
            return "Test{" +
                    "required=" + required +
                    '}';
        }
    }

    /**
     * @param fieldsOrder порядок сортировки параметров
     * @return отсортированные в заданной последовательности параметры вакансии
     * @throws ReflectiveOperationException возникает, если нужного поля не существует, либо оно недоступно
     */
    public List<Object> getFieldsDataList(List<String> fieldsOrder) throws ReflectiveOperationException {
        List<Object> vacancyInfo = new ArrayList<>(fieldsOrder.size());
        for (String fieldName : fieldsOrder) {
            try {
                if (fieldName.isEmpty()) { // Избегаем зануленных пользовательских полей
                    vacancyInfo.add("");
                    continue;
                }
                Object fieldValue = this.getClass().getDeclaredField(fieldName).get(this);
                String tmp = "-";   // прочерк, если данных нет
                if (fieldValue != null && !fieldValue.toString().isEmpty()) {
                    if (!(fieldValue instanceof List)) {
                        tmp = fieldValue.toString();
                    } else if (!((List<?>) fieldValue).isEmpty()) {
                        tmp = fieldValue.toString();
                        tmp = tmp.substring(1, tmp.length() - 1);
                    }
                }
                vacancyInfo.add(tmp);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ReflectiveOperationException("Ошибка при обращении к полям класса Vacancy", e);
            }
        }
        return vacancyInfo;
    }
}
