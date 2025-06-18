package backend.academy.bot.repository.state;

public enum State {
    IDLE, // Нет активного диалога
    AWAITING_LINK, // Ожидается ввод ссылки
    AWAITING_TAGS, // Ожидается ввод тегов
    AWAITING_FILTERS // Ожидается ввод фильтров
}
