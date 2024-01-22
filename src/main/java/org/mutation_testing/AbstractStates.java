package org.mutation_testing;

import java.util.List;
import java.util.ArrayList;

public abstract class AbstractStates {

    private AbstractStates() {
        throw new IllegalStateException("Do not invoke this");
    }

    public static class PuncturedIntervals {

        List<Interval> intervals;

        public PuncturedIntervals() {
            this.intervals = new ArrayList<>();
            intervals.add(new Interval(Long.MIN_VALUE, Long.MAX_VALUE));
        }

        public PuncturedIntervals(Interval... intervals) {
            this.intervals = new ArrayList<>();
            for (Interval interval : intervals) {
                this.intervals.add(interval);
            }
        }

        public void puncture(Long point) {
            if (point == null) {
                throw new IllegalArgumentException("Point cannot be null");
            }
            for (Interval interval : intervals) {
                interval.puncture(point);
            }
        }

        public void splitAt(Long point) {
            if (point == null) {
                throw new IllegalArgumentException("Point cannot be null");
            }
            List<Interval> newIntervals = new ArrayList<>();
            for (Interval interval : intervals) {
                newIntervals.addAll(interval.splitAt(point));
            }
            intervals = newIntervals;
        }

        public List<String> emitConditions(String variableName) {
            List<String> conditions = new ArrayList<>();

            for (Interval interval : intervals) {
                StringBuilder sb = new StringBuilder();
                // Check for singleton point
                if (interval.lowerBound.equals(interval.upperBound)
                        && !interval.punctures.contains(interval.lowerBound)) {
                    sb.append(variableName).append(" == ").append(interval.lowerBound);
                    conditions.add(sb.toString());
                } else {
                    // The general case
                    if (interval.lowerBound != Long.MIN_VALUE) {
                        sb.append(interval.lowerBound).append(" <= ").append(variableName);
                    }
                    if (interval.upperBound != Long.MAX_VALUE) {
                        if (sb.length() > 0) {
                            sb.append(" && ");
                        }
                        sb.append(variableName).append(" <= ").append(interval.upperBound);
                    }
                    for (Long puncture : interval.punctures) {
                        sb.append(" && ").append(variableName).append(" != ").append(puncture);
                    }
                    conditions.add(sb.toString()); // Add main interval case

                    // Now, each puncture
                    for (Long puncture : interval.punctures) {
                        conditions.add(variableName + " == " + puncture);
                    }
                }
            }
            return conditions;
        }

        public static class Interval {
            Long lowerBound;
            Long upperBound;

            /**
             * A sorted list of punctures
             */
            List<Long> punctures;

            public Interval(Long lowerBound, Long upperBound, List<Long> punctures) {
                this.lowerBound = lowerBound;
                this.upperBound = upperBound;
                this.punctures = new ArrayList<>();
                puncture(punctures);
            }

            public Interval(Long lowerBound, Long upperBound) {
                this(lowerBound, upperBound, (List<Long>) null);
            }

            public Interval(Long lowerBound, Long upperBound, Long... punctures) {
                this(lowerBound, upperBound);
                for (Long point : punctures) {
                    puncture(point);
                }
            }

            public boolean puncture(Long point) {
                if (point == null) {
                    throw new IllegalArgumentException("Puncture cannot be null");
                }
                if (point < this.lowerBound || point > this.upperBound) {
                    return false;
                }

                for (int i = 0; i < this.punctures.size(); i++) {
                    if (point.equals(this.punctures.get(i))) {
                        return false;
                    }
                    if (point > this.punctures.get(i)) {
                        this.punctures.add(i, point);
                        return true;
                    }
                }
                this.punctures.add(point);
                return true;
            }

            public void puncture(List<Long> points) {
                if (points == null)
                    return;
                for (Long point : points) {
                    this.puncture(point);
                }
            }

            public List<Interval> splitAt(Long point) {
                if (point == null) {
                    throw new IllegalArgumentException("Point cannot be null");
                }
                List<Interval> intervals = new ArrayList<>();

                if (point < this.lowerBound || point > this.upperBound) {
                    intervals.add(this);
                } else if (point.equals(lowerBound) && point.equals(upperBound)) {
                    intervals.add(new Interval(this.lowerBound, this.upperBound));
                } else {
                    if (point > this.lowerBound) {
                        intervals.add(new Interval(this.lowerBound, point - 1, this.punctures));
                    }
                    intervals.add(new Interval(point, point));
                    if (point < this.upperBound) {
                        intervals.add(new Interval(point + 1, this.upperBound, this.punctures));
                    }
                }
                return intervals;
            }

            public boolean contains(Long point) {
                if (point == null) {
                    throw new IllegalArgumentException("Point cannot be null");
                }
                return point >= this.lowerBound && point <= this.upperBound && !this.punctures.contains(point);
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((lowerBound == null) ? 0 : lowerBound.hashCode());
                result = prime * result + ((upperBound == null) ? 0 : upperBound.hashCode());
                result = prime * result + ((punctures == null) ? 0 : punctures.hashCode());
                return result;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                Interval other = (Interval) obj;
                if (lowerBound == null) {
                    if (other.lowerBound != null)
                        return false;
                } else if (!lowerBound.equals(other.lowerBound))
                    return false;
                if (upperBound == null) {
                    if (other.upperBound != null)
                        return false;
                } else if (!upperBound.equals(other.upperBound))
                    return false;
                if (punctures == null) {
                    if (other.punctures != null)
                        return false;
                } else if (!punctures.equals(other.punctures))
                    return false;
                return true;
            }

        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((intervals == null) ? 0 : intervals.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PuncturedIntervals other = (PuncturedIntervals) obj;
            if (intervals == null) {
                if (other.intervals != null)
                    return false;
            } else if (!intervals.equals(other.intervals))
                return false;
            return true;
        }
    }

}
