package tubs.cs.studienrichtung.featuremodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.prop4j.Node;

import tubs.cs.studienrichtung.featuremodel.Encoder.Mode;

public class EvaluationData {
	public static class EncodingRequest {
		Encoder.Mode mode;
		int n;
		int k;
		
		public EncodingRequest(Mode mode, int size, int k) {
			this.mode = mode;
			this.n = size;
			this.k = k;
		}

		@Override
		public String toString() {
			return mode.toString() + "(" + n + ", " + k + ")";
		}
	}
	
	public static class CompulsoryElectiveConstraint {
		Node constraint;
		Node constraintWithoutImplication;
		List<EncodingRequest> encodingRequests;
		
		CompulsoryElectiveConstraint() {
			encodingRequests = new ArrayList<>();
		}

		public String getEncodingRequestsAsString() {
			String ret = "";
			
			for (int i = 0; i < encodingRequests.size() - 1; ++i) {
				ret += encodingRequests.get(i).toString() + ", ";
			}
			ret += encodingRequests.get(encodingRequests.size() - 1).toString();
			
			return ret;
		}
	}
	
	public Map<String, Integer> encodingUsages = null;
	List<CompulsoryElectiveConstraint> compulsoryElectiveConstraints = null;
	public CompulsoryElectiveConstraint currentConstraint;
	public int visitOptionalConstraint;
	public double configTime;
	public int configTrials;
	
	public EvaluationData() {
		compulsoryElectiveConstraints = new ArrayList<>();
		visitOptionalConstraint = 0;
	}
	
	void pushNewConstraint() {
		currentConstraint = new CompulsoryElectiveConstraint();
		compulsoryElectiveConstraints.add(currentConstraint);
		++visitOptionalConstraint;
	}
}
