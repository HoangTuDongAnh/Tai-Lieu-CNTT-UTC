from calendar import monthrange
from datetime import date, timedelta


VALID_PERIOD_TYPES = {"week", "month", "year"}


def normalize_period(
    period_type: str,
    period_year: int,
    period_month: int | None = None,
    period_week: int | None = None,
) -> dict:
    period_type = period_type.lower().strip()

    if period_type not in VALID_PERIOD_TYPES:
        raise ValueError("period_type must be one of: week, month, year")

    if period_year < 2000 or period_year > 2100:
        raise ValueError("period_year must be between 2000 and 2100")

    if period_type == "week":
        if period_week is None:
            raise ValueError("period_week is required when period_type = 'week'")
        if period_week < 1 or period_week > 53:
            raise ValueError("period_week must be between 1 and 53")

        try:
            start_date = date.fromisocalendar(period_year, period_week, 1)  # Monday
            end_date = start_date + timedelta(days=6)
        except ValueError:
            raise ValueError("Invalid ISO week/year combination")

        return {
            "period_type": "week",
            "period_year": period_year,
            "period_month": None,
            "period_week": period_week,
            "start_date": start_date,
            "end_date": end_date,
        }

    if period_type == "month":
        if period_month is None:
            raise ValueError("period_month is required when period_type = 'month'")
        if period_month < 1 or period_month > 12:
            raise ValueError("period_month must be between 1 and 12")

        last_day = monthrange(period_year, period_month)[1]
        start_date = date(period_year, period_month, 1)
        end_date = date(period_year, period_month, last_day)

        return {
            "period_type": "month",
            "period_year": period_year,
            "period_month": period_month,
            "period_week": None,
            "start_date": start_date,
            "end_date": end_date,
        }

    start_date = date(period_year, 1, 1)
    end_date = date(period_year, 12, 31)

    return {
        "period_type": "year",
        "period_year": period_year,
        "period_month": None,
        "period_week": None,
        "start_date": start_date,
        "end_date": end_date,
    }


def calculate_budget_metrics(limit_amount, spent_amount) -> dict:
    limit_value = float(limit_amount)
    spent_value = float(spent_amount)

    remaining_amount = limit_value - spent_value

    if limit_value <= 0:
        percentage_used = 0
    else:
        percentage_used = round((spent_value / limit_value) * 100, 2)

    if spent_value <= 0:
        status = "normal"
    elif spent_value < limit_value:
        status = "normal"
    elif spent_value == limit_value:
        status = "reached"
    else:
        status = "over"

    return {
        "remaining_amount": remaining_amount,
        "percentage_used": percentage_used,
        "status": status,
    }