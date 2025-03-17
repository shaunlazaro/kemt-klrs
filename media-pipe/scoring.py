from collections import Counter

def score_routine_component(routine_component_data):
    rep_scores = []
    peak_angle = []
    warning = None
    alert_counter = Counter()
    
    # Alert penatly weight (adjustable)
    alert_penalty_factor = 0.2  

    print("REP DATA: ", routine_component_data.rep_data)
    
    for rep_data in routine_component_data.rep_data:
        rep_scores.append(rep_data.max_score)
        
        if routine_component_data.exercise_detail.start_in_flexion:
            peak_angle.append(rep_data.max_extension)
            target_range_of_motion = routine_component_data.exercise_detail.rep_tracking.goal_extension
        else:
            peak_angle.append(rep_data.max_flexion)
            target_range_of_motion = routine_component_data.exercise_detail.rep_tracking.goal_flexion
    
        for alert in rep_data.alerts:
            if alert != routine_component_data.exercise_detail.rep_tracking.alert_message:
                alert_counter[alert] += 1

    most_common_alert, alert_count = alert_counter.most_common(1)[0] if alert_counter else (None, 0)

    if alert_count >= 3:
        warning = most_common_alert
    
    num_reps = len(rep_scores)

    # Compute base score as an average (scaled to 100)
    base_score = (sum(rep_scores) / num_reps) * 100 if num_reps > 0 else 0
    
    # Compute alert penalty factor (based on alerts per rep)
    total_alerts = sum(alert_counter.values())
    alert_penalty = alert_penalty_factor * (total_alerts / num_reps) if num_reps > 0 else 0
    
    # Apply penalty, ensuring score remains non-negative
    exercise_score = max(0, base_score * (1 - alert_penalty))
    
    avg_peak_angle = sum(peak_angle) / num_reps if num_reps > 0 else 0
    
    return exercise_score, avg_peak_angle, target_range_of_motion, warning